import {
  AutoRefreshTokenService,
  provideKeycloak,
  UserActivityService,
  withAutoRefreshToken
} from 'keycloak-angular';

import { environment } from '../../../environments/environment';

export const keycloakConfig = environment.keycloak;

export function provideStudyFlowKeycloak() {
  return provideKeycloak({
    config: keycloakConfig,
    initOptions: {
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
      checkLoginIframe: false
    },
    features: [
      withAutoRefreshToken({
        onInactivityTimeout: 'logout',
        sessionTimeout: 600000
      })
    ],
    providers: [AutoRefreshTokenService, UserActivityService]
  });
}
