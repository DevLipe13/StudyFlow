import { environment } from '../../../environments/environment';

export const API_V1_BASE = `${environment.apiBaseUrl}/api/v1`;

export function createApiUrlPattern(apiBaseUrl: string = environment.apiBaseUrl): RegExp {
  const escaped = apiBaseUrl.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  return new RegExp(`^${escaped}/api/v1(?:/.*)?$`, 'i');
}

export function shouldAttachBearerToken(url: string, apiBaseUrl: string = environment.apiBaseUrl): boolean {
  return createApiUrlPattern(apiBaseUrl).test(url);
}
