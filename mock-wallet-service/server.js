const http = require("http");
const { URL } = require("url");

const port = process.env.PORT || 8081;

const seedFromString = (value) => {
  let hash = 2166136261;
  for (const char of value) {
    hash ^= char.charCodeAt(0);
    hash = Math.imul(hash, 16777619);
  }
  return hash >>> 0;
};

const createRng = (seed) => {
  let state = seed >>> 0;
  return () => {
    state = (state * 1664525 + 1013904223) >>> 0;
    return state / 0xffffffff;
  };
};

const buildWallet = (address) => {
  const rng = createRng(seedFromString(address));
  const nTx = Math.floor(rng() * 5000) + 1;
  const totalReceived = Math.floor(rng() * 5_000_000_000_000);
  const finalBalance = Math.floor(totalReceived * rng());

  return {
    final_balance: finalBalance,
    n_tx: nTx,
    total_received: totalReceived,
  };
};

const sendJson = (res, statusCode, payload) => {
  res.writeHead(statusCode, { "Content-Type": "application/json" });
  res.end(JSON.stringify(payload));
};

const server = http.createServer((req, res) => {
  try {
    const { pathname, searchParams } = new URL(req.url, `http://${req.headers.host}`);

    if (pathname === "/balance") {
      const activeParam = searchParams.get("active");
      if (!activeParam) {
        sendJson(res, 400, { error: "Missing active param" });
        return;
      }

      const addresses = activeParam
        .split(/[|,]/)
        .map((entry) => entry.trim())
        .filter(Boolean);

      if (!addresses.length) {
        sendJson(res, 400, { error: "No addresses provided" });
        return;
      }

      const response = addresses.reduce((acc, address) => {
        acc[address] = buildWallet(address);
        return acc;
      }, {});

      sendJson(res, 200, response);
      return;
    }

    if (pathname === "/") {
      sendJson(res, 200, { status: "ok", service: "mock-wallet-service" });
      return;
    }

    sendJson(res, 404, { error: "Not found" });
  } catch (error) {
    console.error("Error handling request", error);
    sendJson(res, 500, { error: "Internal server error" });
  }
});

server.listen(port, () => {
  console.log(`Mock wallet service listening on port ${port}`);
});
