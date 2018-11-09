module powerworks {
    requires transitive java.desktop;
    requires kotlin.stdlib;
    requires kotlin.stdlib.jdk8;
    requires AudioCue.SNAPSHOT;
    requires gdx;
    requires gdx.freetype;
    requires gdx.backend.lwjgl3;
    requires org.lwjgl.openal;
    uses mod.Mod;
    exports audio;
    exports crafting;
    exports graphics;
    exports io;
    exports item;
    exports level;
    exports level.block;
    exports level.living;
    exports level.moving;
    exports level.tile;
    exports level.tube;
    exports main;
    exports misc;
    exports mod;
    exports resource;
    exports screen;
    exports screen.elements;
    exports weapon;
}