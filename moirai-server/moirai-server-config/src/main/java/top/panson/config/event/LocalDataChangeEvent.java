package top.panson.config.event;

/**
 * Local data change event.
 */
public class LocalDataChangeEvent extends AbstractEvent {

    /**
     * Tenant +  Item + Thread-pool
     */
    public final String groupKey;

    /**
     * Client instance unique identifier
     */
    public final String identify;

    public LocalDataChangeEvent(String identify, String groupKey) {
        this.identify = identify;
        this.groupKey = groupKey;
    }
}