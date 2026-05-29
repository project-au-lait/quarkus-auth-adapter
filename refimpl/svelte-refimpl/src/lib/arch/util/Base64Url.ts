export function base64urlEncode(str: string): string {
  return btoa(str).replaceAll('+', '-').replaceAll('/', '_').replace(/=+$/, '');
}

export function base64urlEncodeBuffer(buf: Uint8Array): string {
  let binary = '';
  for (const byte of buf) {
    binary += String.fromCodePoint(byte);
  }
  return btoa(binary).replaceAll('+', '-').replaceAll('/', '_').replace(/=+$/, '');
}
