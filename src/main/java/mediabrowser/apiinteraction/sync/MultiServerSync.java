package mediabrowser.apiinteraction.sync;

import mediabrowser.apiinteraction.IConnectionManager;
import mediabrowser.apiinteraction.sync.data.ILocalAssetManager;
import mediabrowser.apiinteraction.sync.server.ServerSync;
import mediabrowser.apiinteraction.tasks.CancellationToken;
import mediabrowser.model.apiclient.ServerInfo;
import mediabrowser.model.extensions.ListHelper;
import mediabrowser.model.logging.ILogger;

import java.util.ArrayList;

public class MultiServerSync {

    private IConnectionManager connectionManager;
    private ILogger logger;
    private ILocalAssetManager localAssetManager;
    private String[] cameraUploadServers;

    public MultiServerSync(IConnectionManager connectionManager, ILogger logger, ILocalAssetManager localAssetManager, String[] cameraUploadServers) {
        this.connectionManager = connectionManager;
        this.logger = logger;
        this.localAssetManager = localAssetManager;
        this.cameraUploadServers = cameraUploadServers;
    }

    public void Sync(final CancellationToken cancellationToken, final SyncProgress progress){

        connectionManager.GetAvailableServers(new MultiServerSyncGetServersResponse(this, progress, cancellationToken));
    }

    void SyncNext(final ArrayList<ServerInfo> servers, final int index, final CancellationToken cancellationToken, final SyncProgress progress){

        if (index >= servers.size()){

            progress.reportComplete();
            return;
        }

        if (cancellationToken.isCancellationRequested()){
            progress.reportCancelled();
        }

        ServerInfo server = servers.get(index);
        final int numServers = servers.size();
        final double[] numComplete = {0};

        ServerInfo freshServerInfo = connectionManager.getServerInfo(server.getId());

        if (freshServerInfo != null){
            server = freshServerInfo;
        }

        boolean uploadPhotos = ListHelper.ContainsIgnoreCase(cameraUploadServers, server.getId());
        new ServerSync(connectionManager, logger, localAssetManager).Sync(server, uploadPhotos, cancellationToken, new MultiServerSyncProgress(this, servers, cancellationToken, index, numServers, numComplete, progress));
    }
}
