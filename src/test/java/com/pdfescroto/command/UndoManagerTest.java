package com.pdfescroto.command;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UndoManagerTest {

    /** Simple command that appends/removes from a list */
    static class AppendCommand implements Command {
        private final List<String> list;
        private final String value;
        AppendCommand(List<String> list, String value) { this.list = list; this.value = value; }
        @Override public void execute() { list.add(value); }
        @Override public void undo()    { list.remove(list.size() - 1); }
    }

    @Test
    void executeRunsCommand() {
        var list = new ArrayList<String>();
        var mgr  = new UndoManager(10);
        mgr.execute(new AppendCommand(list, "a"));
        assertEquals(List.of("a"), list);
    }

    @Test
    void undoReversesCommand() {
        var list = new ArrayList<String>();
        var mgr  = new UndoManager(10);
        mgr.execute(new AppendCommand(list, "a"));
        mgr.undo();
        assertTrue(list.isEmpty());
    }

    @Test
    void redoReappliesCommand() {
        var list = new ArrayList<String>();
        var mgr  = new UndoManager(10);
        mgr.execute(new AppendCommand(list, "a"));
        mgr.undo();
        mgr.redo();
        assertEquals(List.of("a"), list);
    }

    @Test
    void newCommandClearsRedoStack() {
        var list = new ArrayList<String>();
        var mgr  = new UndoManager(10);
        mgr.execute(new AppendCommand(list, "a"));
        mgr.undo();
        mgr.execute(new AppendCommand(list, "b"));
        assertFalse(mgr.canRedo());
    }

    @Test
    void undoWhenEmptyDoesNothing() {
        var mgr = new UndoManager(10);
        assertDoesNotThrow(mgr::undo);
    }

    @Test
    void redoWhenEmptyDoesNothing() {
        var mgr = new UndoManager(10);
        assertDoesNotThrow(mgr::redo);
    }

    @Test
    void boundedStack_dropsOldestWhenFull() {
        var list = new ArrayList<String>();
        var mgr  = new UndoManager(3);
        mgr.execute(new AppendCommand(list, "a"));
        mgr.execute(new AppendCommand(list, "b"));
        mgr.execute(new AppendCommand(list, "c"));
        mgr.execute(new AppendCommand(list, "d")); // should drop "a"
        // Can only undo 3 times
        mgr.undo(); mgr.undo(); mgr.undo();
        assertFalse(mgr.canUndo());
    }
}
