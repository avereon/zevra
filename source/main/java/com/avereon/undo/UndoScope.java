package com.avereon.undo;

public interface UndoScope {

	/**
	 * Is there a change that can be undone.
	 */
	boolean canUndo();

	/**
	 * Undo the most recent change, if there is any change to undo.
	 *
	 * @return true if a change was undone, false otherwise.
	 */
	boolean undo();

	/**
	 * Is there a change that can be redone.
	 */
	boolean canRedo();

	/**
	 * Redo previously undone change, if there is any change to redo.
	 *
	 * @return true if a change was redone, false otherwise.
	 */
	boolean redo();

	/**
	 * Forgets all changes prior to the current position in the history.
	 */
	void purgeHistory();

	/**
	 * Forgets all changes after the current position in the history.
	 */
	void purgeFuture();

}
