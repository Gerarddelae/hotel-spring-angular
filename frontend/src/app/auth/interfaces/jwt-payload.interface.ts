export interface JwtPayload {
  sub: string;
  authorities: Authority[];
  iat?: number;
  exp?: number;
}

export interface Authority {
  authority: string;
}

