/*
 * Copyright (c) 2019, Fraser Killip <https://github.com/FraserKillip>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.fraser;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Locale;
import javax.annotation.Nullable;
import javax.inject.Inject;
import joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.IndexedSprite;
import net.runelite.api.MessageNode;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
        name = "Twitch Emotes",
        description = "Adds a few common emotes from BTTV and FFZ",
        enabledByDefault = true
)
@Slf4j
public class EmotesPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ChatMessageManager chatMessageManager;

    private int modIconsStart = -1;

    @Override
    protected void startUp()
    {
        loadEmojiIcons();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
        {
            loadEmojiIcons();
        }
    }

    private void loadEmojiIcons()
    {
        final IndexedSprite[] modIcons = client.getModIcons();
        if (modIconsStart != -1 || modIcons == null)
        {
            return;
        }

        final Emote[] emotes = Emote.values();
        final IndexedSprite[] newModIcons = Arrays.copyOf(modIcons, modIcons.length + emotes.length);
        modIconsStart = modIcons.length;

        for (int i = 0; i < emotes.length; i++)
        {
            final Emote emote = emotes[i];

            try
            {
                final BufferedImage image = emote.loadImage();

                log.warn("Emoji: " + emote.name() + " width: " + image.getWidth() + ", height: " + image.getHeight());

                final IndexedSprite sprite = ImageUtil.getImageIndexedSprite(image, client);
                newModIcons[modIconsStart + i] = sprite;
            }
            catch (Exception ex)
            {
                log.warn("Failed to load the sprite for emoji " + emote, ex);
            }
        }

        log.debug("Adding emoji icons");
        client.setModIcons(newModIcons);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage)
    {
        if (client.getGameState() != GameState.LOGGED_IN || modIconsStart == -1)
        {
            return;
        }

        switch (chatMessage.getType())
        {
            case PUBLICCHAT:
            case MODCHAT:
            case FRIENDSCHAT:
            case PRIVATECHAT:
            case PRIVATECHATOUT:
            case CLAN_CHAT:
            case CLAN_GUEST_CHAT:
                break;
            default:
                return;
        }

        final String message = chatMessage.getMessage();
        final String updatedMessage = updateMessage(message);

        if (updatedMessage == null)
        {
            return;
        }

        final MessageNode messageNode = chatMessage.getMessageNode();
        messageNode.setRuneLiteFormatMessage(updatedMessage);
        chatMessageManager.update(messageNode);
        client.refreshChat();
    }

    @Subscribe
    public void onOverheadTextChanged(final OverheadTextChanged event)
    {
        if (!(event.getActor() instanceof Player))
        {
            return;
        }

        final String message = event.getOverheadText();
        final String updatedMessage = updateMessage(message);

        if (updatedMessage == null)
        {
            return;
        }

        event.getActor().setOverheadText(updatedMessage);
    }

    @Nullable
    private String updateMessage(final String message)
    {

        final String[] messageWords = message.split(" ");

        boolean editedMessage = false;
        for (int i = 0; i < messageWords.length; i++)
        {
            final Emote emote = Emote.getEmote(messageWords[i].toLowerCase(Locale.ROOT));

            if (emote == null)
            {
                continue;
            }

            final int emojiId = modIconsStart + emote.ordinal();

            messageWords[i] = new ChatMessageBuilder().img(emojiId).build();

            editedMessage = true;
        }

        // If we haven't edited the message any, don't update it.
        if (!editedMessage)
        {
            return null;
        }

        return Strings.join(messageWords, " ");
    }
}