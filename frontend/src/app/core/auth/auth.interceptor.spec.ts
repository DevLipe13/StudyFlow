import { createApiUrlPattern, shouldAttachBearerToken } from '../api/api.constants';

describe('auth interceptor helper', () => {
  it('should match api v1 urls', () => {
    const pattern = createApiUrlPattern('http://localhost:8080');
    expect(pattern.test('http://localhost:8080/api/v1/users')).toBeTrue();
    expect(pattern.test('http://localhost:8080/api/v2/users')).toBeFalse();
  });

  it('should decide bearer attachment by url', () => {
    expect(shouldAttachBearerToken('http://localhost:8080/api/v1/users/me')).toBeTrue();
    expect(shouldAttachBearerToken('https://external.example/api/v1/users')).toBeFalse();
  });
});
