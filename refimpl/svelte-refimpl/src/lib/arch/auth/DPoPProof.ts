import { getKeyPair, getJwk } from './DPoPKeyStore';
import { base64urlEncode, base64urlEncodeBuffer } from '../util/Base64Url';

let serverNonce: string | undefined;

export function updateNonce(nonce: string): void {
  serverNonce = nonce;
}

export function getNonce(): string | undefined {
  return serverNonce;
}

export interface DPoPOverrides {
  htu?: string;
  htm?: string;
}

export async function buildDPoPProof(
  input: RequestInfo | URL,
  init?: RequestInit,
  accessToken?: string,
  overrides?: DPoPOverrides
): Promise<string> {
  const kp = await getKeyPair();
  const jwk = await getJwk();

  const url = input instanceof Request ? new URL(input.url) : new URL(input.toString());
  const htu = overrides?.htu ?? `${url.origin}${url.pathname}`;
  const htm =
    overrides?.htm ??
    (input instanceof Request ? input.method : (init?.method ?? 'GET')).toUpperCase();
  const ath = accessToken ? await computeAth(accessToken) : undefined;

  const header = {
    typ: 'dpop+jwt',
    alg: 'ES256',
    jwk: { kty: jwk.kty, crv: jwk.crv, x: jwk.x, y: jwk.y }
  };

  const payload: Record<string, unknown> = {
    jti: crypto.randomUUID(),
    htm,
    htu,
    iat: Math.floor(Date.now() / 1000),
    ath,
    nonce: serverNonce
  };

  const encodedHeader = base64urlEncode(JSON.stringify(header));
  const encodedPayload = base64urlEncode(JSON.stringify(payload));
  const signingInput = `${encodedHeader}.${encodedPayload}`;

  const signature = await crypto.subtle.sign(
    { name: 'ECDSA', hash: 'SHA-256' },
    kp.privateKey,
    new TextEncoder().encode(signingInput)
  );

  const encodedSignature = base64urlEncodeBuffer(new Uint8Array(signature));

  return `${signingInput}.${encodedSignature}`;
}

async function computeAth(accessToken: string): Promise<string> {
  const digest = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(accessToken));
  return base64urlEncodeBuffer(new Uint8Array(digest));
}
