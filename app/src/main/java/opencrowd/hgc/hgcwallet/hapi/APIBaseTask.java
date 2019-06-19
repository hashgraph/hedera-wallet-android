package opencrowd.hgc.hgcwallet.hapi;

import android.support.annotation.NonNull;

import com.hederahashgraph.service.proto.java.CryptoServiceGrpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import opencrowd.hgc.hgcwallet.App;
import opencrowd.hgc.hgcwallet.Config;
import opencrowd.hgc.hgcwallet.common.BaseTask;
import opencrowd.hgc.hgcwallet.common.Singleton;
import opencrowd.hgc.hgcwallet.database.node.Node;

public abstract class APIBaseTask extends BaseTask {
    public Node node;

    public Logger LOG = LoggerFactory.getLogger(this.getClass());

    public APIBaseTask() {

    }

    @Override
    public void main() {
        this.node = App.instance.addressBook.randomNode();
        log("Node >>> " + node.getHost());
    }

    public ManagedChannel channel(){
        return ManagedChannelBuilder.forAddress(node.getHost(),node.getPort()).usePlaintext().build();
    }

    @NonNull
    public CryptoServiceGrpc.CryptoServiceBlockingStub cryptoStub (){
        return CryptoServiceGrpc.newBlockingStub(channel());
    }

    public String getMessage(Exception e) {
        if (e instanceof StatusRuntimeException) {
            StatusRuntimeException statusRuntimeException = (StatusRuntimeException) e;
            Status status = statusRuntimeException.getStatus();
            switch (status.getCode()) {
                case OK:
                    break;
                case CANCELLED:
                    break;
                case UNKNOWN:
                case UNAVAILABLE:
                case DEADLINE_EXCEEDED:
                    return "Node is not reachable: " + node.getHost();
                case INVALID_ARGUMENT:
                    break;
                case NOT_FOUND:
                    break;
                case ALREADY_EXISTS:
                    break;
                case PERMISSION_DENIED:
                    break;
                case RESOURCE_EXHAUSTED:
                    break;
                case FAILED_PRECONDITION:
                    break;
                case ABORTED:
                    break;
                case OUT_OF_RANGE:
                    break;
                case UNIMPLEMENTED:
                    break;
                case INTERNAL:
                    break;
                case DATA_LOSS:
                    break;
                case UNAUTHENTICATED:
                    break;
                default:
                    break;

            }
        }
        return e.getMessage();
    }

    public void log(String message) {
        if (Config.isLoggingEnabled) {
            LOG.debug(message);
            Singleton.INSTANCE.getApiLogs().append(message);
        }
    }
}
