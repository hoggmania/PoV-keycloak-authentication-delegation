package org.keycloak.examples.userstorage.readonly;

import java.util.List;

//import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

public class DelegatedUserStorageProviderFactory implements UserStorageProviderFactory<DelegatedUserStorageProvider>  {
	// WIP
	
    //private static final Logger logger = Logger.getLogger(DelegatedUserStorageProviderFactory.class);

	// TODO: if considering internationalization, need to move text onto message file for each locale.
	
    public static final String PROVIDER_NAME = "readonly-delegated-server";
 
    protected static final List<ProviderConfigProperty> configMetadata;

    public static final String AS_USERINFO_URI = "external.authentication.server.userinfo.uri";
    
    static {
        configMetadata = ProviderConfigurationBuilder.create()
                .property().name(AS_USERINFO_URI)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("External Authentication Server User Information URI")
                .helpText("Endpoint for obtaining User Information by User ID.")
                .add().build();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }
    
    @Override
    public String getId() {
		// TODO Auto-generated method stub
        return PROVIDER_NAME;
    }


    @Override
    public void init(Config.Scope config) {
		// TODO Auto-generated method stub
    }

    @Override
    public DelegatedUserStorageProvider create(KeycloakSession session, ComponentModel model) {
		// TODO Auto-generated method stub
        return new DelegatedUserStorageProvider(session, model);
    }
}
