package com.avereon.undo;

import java.util.Objects;

public class BasicUndoScope implements UndoScope {

	@Override
	public boolean canUndo() {
		return false;
	}

	@Override
	public boolean undo() {
		return false;
	}

	@Override
	public boolean canRedo() {
		return false;
	}

	@Override
	public boolean redo() {
		return false;
	}

	@Override
	public void purgeHistory() {

	}

	@Override
	public void purgeFuture() {

	}

	@Override
	public int hashCode() {
		return Objects.hash( this );
	}
}
