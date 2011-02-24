package net.threescale.api.cache;

import net.threescale.api.LogFactory;
import net.threescale.api.v2.*;
import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.Region;
import org.jboss.cache.config.EvictionRegionConfig;
import org.jboss.cache.eviction.LRUAlgorithmConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CacheImplCommon implements ApiCache {

    public static final String authorize_prefix = "/authorize";
    public static final String authorizeResponseKey = "/auth_response";

    public static final String responseKey = "/response";

    private Logger log = LogFactory.getLogger(this);
    
    private HttpSender sender;
    private String host_url;
    private String provider_key;

    // This is initialized by sub-class
    private Cache data_cache;

    private long authorizeExpirationTimeInMillis = 500L;
    private long reportExpirationTimeInMillis = 500L;

    public CacheImplCommon(String host_url, String provider_key, HttpSender sender, Cache cache) {
        this.sender = sender;
        this.host_url = host_url;
        this.provider_key = provider_key;
        this.data_cache = cache;
        addEvictionPolicies(data_cache);
    }

     public AuthorizeResponse getAuthorizeFor(String app_key) {
        Fqn<String> authorizeFqn = Fqn.fromString(authorize_prefix + "/" + app_key);
        return (AuthorizeResponse) data_cache.get(authorizeFqn, authorizeResponseKey);
    }


    public ApiTransaction getTransactionFor(String app_id, String when) {
        Fqn<String> reportFqn = Fqn.fromString(responseKey + "/" + app_id);
        return (ApiTransaction) data_cache.get(reportFqn, when);
    }

    public void addAuthorizedResponse(String app_key, AuthorizeResponse authorizedResponse) {
        Fqn<String> authorizeFqn = Fqn.fromString(authorize_prefix + "/" + app_key);
        Node root = data_cache.getRoot();
        Node authorizeNode = data_cache.getNode(authorizeFqn);
        if (authorizeNode == null) {
            authorizeNode = root.addChild(authorizeFqn);
        }

        Long future = System.currentTimeMillis() + authorizeExpirationTimeInMillis;
        authorizeNode.put(authorizeResponseKey, authorizedResponse);
        authorizeNode.put("expiration", future);
    }

    public void close() {
        data_cache.stop();
        data_cache.destroy();
    }


    public void setAuthorizeExpirationInterval(long expirationTimeInMillis) {
        this.authorizeExpirationTimeInMillis = expirationTimeInMillis;
    }


    public void setReportExpirationInterval(long expirationTimeInMillis) {
        this.reportExpirationTimeInMillis = expirationTimeInMillis;
    }


    public void report(ApiTransaction[] transactions) throws ApiException {
        for (ApiTransaction transaction : transactions) {
            Fqn<String> reportFqn = Fqn.fromString(responseKey + "/" + transaction.getApp_id());

            Node reportNode = data_cache.getNode(reportFqn);
            if (reportNode == null) {
                reportNode = data_cache.getRoot().addChild(reportFqn);
            }

            reportNode.put(transaction.getTimestamp(), transaction);

            log.fine("Put transaction into cache as " + reportFqn + "/" + transaction.getTimestamp());
        }
    }
    /* Setup the Eviction policy for the response nodes
       Called after the cache has been created
     */
    private void addEvictionPolicies(Cache cache) {
        Fqn fqn = Fqn.fromString(authorizeResponseKey);

        // Create a configuration for an LRUPolicy
        LRUAlgorithmConfig lruc = new LRUAlgorithmConfig();
        lruc.setMaxNodes(10000);

        // Create an eviction region config
        EvictionRegionConfig erc = new EvictionRegionConfig(fqn, lruc);


        // Create the region and set the config
        Region region = cache.getRegion(fqn, true);
        region.setEvictionRegionConfig(erc);
    }

}
