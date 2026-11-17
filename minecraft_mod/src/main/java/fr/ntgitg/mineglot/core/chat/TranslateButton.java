package fr.ntgitg.mineglot.core.chat;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Appends a clickable [T] after every chat message. Clicking it runs "/mgt <id>", which
 * translates the stored message (always treated as Russian) into English.
 * Texts are kept in memory (not in the command) because 1.8.9 limits commands to 100 chars.
 */
public final class TranslateButton {

    public static final String COMMAND = "mgt";
    private static final int MAX_STORED = 400;
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);
    private static final Pattern LOOSE_CHAT = Pattern.compile(
            "^\\W*(?:\\[[^\\]]*\\]\\s*)*(?:<)?[A-Za-z0-9_]{2,16}(?:>)?\\s*[:>\u00BB]\\s*(.+)$");

    private static final Map<Integer, String> TEXTS =
            new LinkedHashMap<Integer, String>(512, 0.75f, false) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
                    return size() > MAX_STORED;
                }
            };

    private TranslateButton() {
    }

    /** Adds " [T]" to the end of the message. Safe to call with any message. */
    public static void attach(IChatComponent message) {
        if (message == null) {
            return;
        }
        String raw = message.getUnformattedText();
        if (raw == null) {
            return;
        }
        raw = raw.trim();
        if (raw.isEmpty() || raw.startsWith("/")) {
            return;
        }

        // Translate only what was said (after "Name:"), not the "[Rank] Name:" prefix.
        String body = raw;
        Matcher m = LOOSE_CHAT.matcher(raw);
        if (m.find() && !m.group(1).trim().isEmpty()) {
            body = m.group(1).trim();
        }

        int id = NEXT_ID.getAndIncrement();
        synchronized (TEXTS) {
            TEXTS.put(id, body);
        }

        ChatComponentText button = new ChatComponentText(" \u00A78[\u00A7bT\u00A78]");
        ChatStyle style = new ChatStyle();
        style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + COMMAND + " " + id));
        style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ChatComponentText("\u00A77Translate this message to English")));
        button.setChatStyle(style);
        message.appendSibling(button);
    }

    public static String getText(int id) {
        synchronized (TEXTS) {
            return TEXTS.get(id);
        }
    }
}
