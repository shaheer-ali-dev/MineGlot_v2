package fr.ntgitg.mineglot.events.chat;

import fr.ntgitg.mineglot.core.chat.ChatMessageParser;
import fr.ntgitg.mineglot.core.chat.ChatMessageParser.ParsedChat;
import fr.ntgitg.mineglot.core.chat.ChatTranslationFilter;
import fr.ntgitg.mineglot.core.chat.TranslateButton;
import fr.ntgitg.mineglot.core.translation.TranslationOrchestrator;
import fr.ntgitg.mineglot.core.service.chat.ChatMessageManager;
import fr.ntgitg.mineglot.utils.log.ModLogger;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatMessageInterceptor {

    private static final Pattern CYRILLIC = Pattern.compile("[\\u0400-\\u04FF]");
    // "[Rank] Name: text", "Name > text", "Name \u00BB text" -> group(1)=name, group(2)=text
    private static final Pattern LOOSE_CHAT = Pattern.compile(
            "^\\W*(?:\\[[^\\]]*\\]\\s*)*(?:<)?([A-Za-z0-9_]{2,16})(?:>)?\\s*[:>\u00BB]\\s*(.+)$");

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChat(ClientChatReceivedEvent event) {
        if (event.isCanceled()) {
            return;
        }

        if (!ChatTranslationFilter.canTranslateAnyChatMessage()) {
            return;
        }

        if (event.message == null) {
            ModLogger.warn("Event message was null, chat interception aborted");
            return;
        }

        String rawMessage = event.message.getUnformattedText();
        ParsedChat parsed = ChatMessageParser.extract(event);

        boolean hasCyrillic = rawMessage != null && CYRILLIC.matcher(rawMessage).find();
        if (hasCyrillic) {
            ModLogger.info("[ARGOS] Russian chat line seen: '{}' (parsed={})", rawMessage,
                    parsed != null);
        }
        if (parsed == null && hasCyrillic) {
            // Sender not recognised (custom chat format / tab list): translate the whole line anyway.
            Matcher m = LOOSE_CHAT.matcher(rawMessage.trim());
            if (m.find()) {
                parsed = new ParsedChat(m.group(1), m.group(2).trim());
            } else {
                parsed = new ParsedChat("Chat", rawMessage.trim());
            }
            ModLogger.info("[ARGOS] Fallback parse -> sender='{}'", parsed.pseudo);
        }

        if (parsed == null
                || !ChatTranslationFilter.needsTranslation(parsed.pseudo, parsed.message, rawMessage)) {
            if (hasCyrillic) {
                ModLogger.info("[ARGOS] Russian line NOT translated (filtered out): '{}'", rawMessage);
            }
            return;
        }

        ChatMessageManager chatMessageManager = ChatMessageManager.getInstance();
        ChatMessageManager.PendingMessage pending =
                chatMessageManager.registerPendingMessage(parsed.message, parsed.pseudo);

        event.setCanceled(true);
        TranslateButton.attach(event.message);
        chatMessageManager.displayOriginalMessage(event.message, pending.getChatLineId());

        try {
            // La detection de langue est resolue une seule fois en aval (apres le
            // fast-path cache), pour eviter une double detection Lingua sur cache hit.
            CompletableFuture<Void> future = TranslationOrchestrator.translateAsync(
                    parsed.pseudo,
                    parsed.message,
                    true,
                    false
            );

            future.thenRun(() -> {
                chatMessageManager.removePendingMessage(pending.getId());
                ModLogger.debug("Traduction reussie, message original supprime: ID={}",
                        pending.getId());
            }).exceptionally(throwable -> {
                ModLogger.error("Traduction echouee, message original conserve: ID={}",
                        pending.getId(), throwable);
                chatMessageManager.releasePendingMessage(pending.getId());
                return null;
            });
        } catch (Exception e) {
            ModLogger.error("Erreur lors du lancement de la traduction", e);
            chatMessageManager.releasePendingMessage(pending.getId());
        }
    }
}
