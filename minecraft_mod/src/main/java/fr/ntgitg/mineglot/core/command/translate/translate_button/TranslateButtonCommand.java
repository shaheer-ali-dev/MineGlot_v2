package fr.ntgitg.mineglot.core.command.translate.translate_button;

import fr.ntgitg.mineglot.core.chat.TranslateButton;
import fr.ntgitg.mineglot.core.command.base.AbstractCommand;
import fr.ntgitg.mineglot.core.model.SupportedLanguage;
import fr.ntgitg.mineglot.core.translation.TranslationOrchestrator;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

/** Internal command behind the [T] button: /mgt <id> -> translate that message (Russian) to English. */
public class TranslateButtonCommand extends AbstractCommand {

    public TranslateButtonCommand() {
        super(TranslateButton.COMMAND, "Translate a chat message (used by the [T] button)", "<id>");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        String text = null;
        if (args != null && args.length > 0) {
            try {
                text = TranslateButton.getText(Integer.parseInt(args[0].trim()));
            } catch (NumberFormatException ignored) {
                // handled below
            }
        }
        if (text == null || text.isEmpty()) {
            sender.addChatMessage(new ChatComponentText(
                    "\u00A7c[MineGlot] This message is too old to translate."));
            return;
        }
        // targeted=true -> English (default language), command=true -> shown only to you.
        // RUSSIAN is forced as the source because you know the message is Russian.
        TranslationOrchestrator.translate(sender.getName(), text, true, true,
                SupportedLanguage.RUSSIAN);
    }
}
