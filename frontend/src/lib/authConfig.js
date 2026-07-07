export const authConfig = {
  clientId: "planmate-frontend",
  authorizationEndpoint: `${
    import.meta.env.VITE_KEYCLOAK_URL
  }/realms/planmate/protocol/openid-connect/auth`,
  tokenEndpoint: `${
    import.meta.env.VITE_KEYCLOAK_URL
  }/realms/planmate/protocol/openid-connect/token`,
  redirectUri: window.location.origin,
  scope: "openid profile email offline_access",
  onRefreshTokenExpire: () => {
    localStorage.clear();
    window.location.href = "/";
  },
  autoLogin: false,
  clearURL: true,
  tokenExpiresIn: 300,
  refreshTokenExpiresIn: 1800, //30 minutes
};
