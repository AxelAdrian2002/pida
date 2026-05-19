import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../environments/environment';

const ABSOLUTE_URL = /^https?:\/\//i;

export const apiBaseUrlInterceptor: HttpInterceptorFn = (req, next) => {
  // Only rewrite app API calls that are relative (/api/...).
  if (!req.url.startsWith('/api/') || ABSOLUTE_URL.test(req.url)) {
    return next(req);
  }

  const apiBaseUrl = (environment.apiBaseUrl || '').trim().replace(/\/$/, '');
  if (!apiBaseUrl) {
    return next(req);
  }

  const nextReq = req.clone({
    url: `${apiBaseUrl}${req.url}`
  });

  return next(nextReq);
};
