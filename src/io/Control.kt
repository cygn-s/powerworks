package io

enum class Control {
    UP, DOWN, LEFT, RIGHT, TOGGLE_INVENTORY,

    INTERACT, SHIFT_INTERACT, CONTROL_INTERACT, ALT_INTERACT, SCROLL_UP, SCROLL_DOWN, SECONDARY_INTERACT,

    /* Debug */
    DEBUG,
    TOGGLE_RENDER_HITBOXES,
    TOGGLE_CHUNK_INFO,
    TOGGLE_DEBUG_TUBE_GROUP_INFO,
    TOGGLE_SCREEN_DEBUG_INFO,
    TOGGLE_RESOURCE_NODES_INFO,

    /* Hotbar slots */
    SLOT_1, SLOT_2, SLOT_3, SLOT_4, SLOT_5, SLOT_6, SLOT_7, SLOT_8,

    TAKE_SCREENSHOT,

    TOGGLE_VIEW_CONTROLS,
    TOGGLE_MOVEMENT_TOOLS,

    GIVE_TEST_ITEM,
    DROP_HELD_ITEM,
    ROTATE_BLOCK,
    PICK_UP_DROPPED_ITEMS;

    enum class Group(vararg val controls: Control) {
        /* GUI clicks/scrolls, level clicks, etc */
        INTERACTION(INTERACT, SHIFT_INTERACT, CONTROL_INTERACT, ALT_INTERACT, SCROLL_UP, SCROLL_DOWN, SECONDARY_INTERACT),
        DEBUG(Control.DEBUG, TOGGLE_CHUNK_INFO, TOGGLE_DEBUG_TUBE_GROUP_INFO, TOGGLE_RENDER_HITBOXES),
        HOTBAR_SLOTS(SLOT_1, SLOT_2, SLOT_3, SLOT_4, SLOT_5, SLOT_6,SLOT_7, SLOT_8),
        SCROLL(SCROLL_DOWN, SCROLL_UP);
        fun contains(c: Control): Boolean = controls.contains(c)
    }

}

