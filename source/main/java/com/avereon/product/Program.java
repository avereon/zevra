package com.avereon.product;

public interface Program extends ProgramProduct {

	//	// THREAD JavaFX-Launcher
	//	// EXCEPTIONS Handled by the FX framework
	//	void init() throws Exception;
	//
	//	// THREAD JavaFX Application Thread
	//	// EXCEPTIONS Handled by the FX framework
	//	boolean requestExit( boolean skipChecks );
	//
	//	boolean requestExit( boolean skipVerifyCheck, boolean skipKeepAliveCheck );
	//
	//	boolean isRunning();
	//
	//	// THREAD JavaFX Application Thread
	//	// EXCEPTIONS Handled by the FX framework
	//	void stop() throws Exception;
	//
	//	boolean isHardwareRendered();
	//
	//	boolean isUpdateInProgress();
	//
	//	void setUpdateInProgress( boolean updateInProgress );
	//
	//	com.avereon.util.Parameters getProgramParameters();
	//
	//	void setProgramParameters( com.avereon.util.Parameters parameters );
	//
	//	String getProfile();
	//
	//	/**
	//	 * Get the home folder. If the home folder is null that means that the program is not installed locally and was most likely started with a technology like
	//	 * Java Web Start.
	//	 *
	//	 * @return The home folder
	//	 */
	//	Path getHomeFolder();
	//
	//	boolean isProgramUpdated();
	//
	//	@Override
	//	ProductCard getCard();
	//
	//	Path getDataFolder();
	//
	//	Path getLogFolder();
	//
	//	Path getTempFolder();
	//
	////	UpdateManager getUpdateManager();
	////
	////	IconLibrary getIconLibrary();
	////
	////	ActionLibrary getActionLibrary();
	////
	////	SettingsManager getSettingsManager();
	//
	//	@Override
	//	Settings getSettings();
	//
	////	TaskManager getTaskManager();
	////
	////	ToolManager getToolManager();
	////
	////	AssetManager getAssetManager();
	////
	////	ThemeManager getThemeManager();
	////
	////	WorkspaceManager getWorkspaceManager();
	////
	////	ProductManager getProductManager();
	////
	////	NoticeManager getNoticeManager();
	////
	////	IndexService getIndexService();
	//
	//	<T extends Event> EventHub register( EventType<? super T> type, EventHandler<? super T> handler );
	//
	//	<T extends Event> EventHub unregister( EventType<? super T> type, EventHandler<? super T> handler );
	//
	//	/**
	//	 * This implementation only returns the product card name.
	//	 */
	//	@Override
	//	String toString();
	//
	//	Path getHomeFromLauncherPath();

}
