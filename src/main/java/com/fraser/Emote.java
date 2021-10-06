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

import com.google.common.collect.ImmutableMap;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import net.runelite.client.util.ImageUtil;

enum Emote
{
    PRAYGE("prayge"),
    GZ("gz"),
    POG("pog"),
    VVEGA("vvega"),
    WIDEPEEPOHAPPY("widepeepohappy"),
    X0TAB("x0tab"),
    X0R6ZTGIGGLE("x0r6ztgiggle"),
    KEKW("kekw"),
    WIDEHARD("widehard");

    private static final Map<String, Emote> emojiMap;

    private final String trigger;

    static
    {
        ImmutableMap.Builder<String, Emote> builder = new ImmutableMap.Builder<>();

        for (final Emote emote : values())
        {
            builder.put(emote.trigger, emote);
        }

        emojiMap = builder.build();
    }

    Emote(String trigger)
    {
        this.trigger = trigger;
    }

    BufferedImage loadImage()
    {
        BufferedImage bufferedImage = ImageUtil.loadImageResource(getClass(), this.name().toLowerCase() + ".png");

        return ImageUtil.bufferedImageFromImage(bufferedImage.getScaledInstance(-1, 13, Image.SCALE_FAST));
    }

    static Emote getEmote(String trigger)
    {
        return emojiMap.get(trigger);
    }
}