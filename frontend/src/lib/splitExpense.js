export function toCents(value) {
  const amount = parseFloat(value);
  if (!Number.isFinite(amount)) return 0;
  return Math.round(amount * 100);
}

export function fromCents(cents) {
  return (cents / 100).toFixed(2);
}

export function splitEqually(totalCents, count) {
  if (count <= 0) return [];
  const base = Math.floor(totalCents / count);
  const remainder = totalCents - base * count;
  return Array.from({ length: count }, (_, i) => base + (i < remainder ? 1 : 0));
}

export function sumCents(shareStrings) {
  return shareStrings.reduce((total, share) => total + toCents(share), 0);
}
