export const authConfig = {
  clientId: "planmate-client",
  authorizationEndpoint: `${
    import.meta.env.VITE_KEYCLOAK_URL
  }/realms/planmate/protocol/openid-connect/auth`,
  tokenEndpoint: `${
    import.meta.env.VITE_KEYCLOAK_URL
  }/realms/planmate/protocol/openid-connect/token`,
  redirectUri: "http://localhost:5173",
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
