/**
 * Vietnam mobile: 10 digits, 0 + (3|5|7|8|9) + 8 digits. Accepts +84 / 84 prefix.
 */
function normalizeVnPhone(raw) {
    if (raw == null) return "";
    let s = String(raw).trim().replace(/[\s.\-()]/g, "");
    if (s.startsWith("+84")) {
        s = "0" + s.slice(3);
    } else if (s.startsWith("84") && s.length >= 10) {
        s = "0" + s.slice(2);
    }
    return s;
}

function isValidVnPhone(raw) {
    const n = normalizeVnPhone(raw);
    if (!n) return false;
    return /^0[35789]\d{8}$/.test(n);
}
