package fr.ntgitg.mineglot.events.chat;

import fr.ntgitg.mineglot.core.chat.TranslateButton;
import fr.ntgitg.mineglot.utils.log.ModLogger;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Adds the [T] button to every normal chat line that is not already handled. */
public class ChatTranslateButtonHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChat(ClientChatReceivedEvent event) {
        if (event.isCanceled() || event.message == null || event.type == 2) {
            return; // type 2 = action bar
        }
        try {
            TranslateButton.attach(event.message);
        } catch (Exception e) {
            ModLogger.warn("[T] button could not be attached", e);
        }
    }
}
