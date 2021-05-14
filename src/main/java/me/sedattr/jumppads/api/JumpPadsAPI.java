package me.sedattr.jumppads.api;

import me.sedattr.jumppads.PadHandler;
import me.sedattr.jumppads.other.Variables;

import java.util.List;

public class JumpPadsAPI {
    public static List<PadHandler> getJumpPads() {
        return Variables.jumpPads;
    }

    public static PadHandler getByName(String name) {
        if (name == null || name.equals(""))
            return null;

        for (PadHandler jumpPad : Variables.jumpPads) {
            String jumpPadName = jumpPad.getName();
            if (jumpPadName == null || jumpPadName.equals(""))
                continue;

            if (jumpPadName.equalsIgnoreCase(name))
                return jumpPad;
        }

        return null;
    }
}
