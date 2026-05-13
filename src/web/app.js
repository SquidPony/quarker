const statusNode = document.getElementById("status");
const viewportNode = document.getElementById("viewport");

function setStatus(message) {
  statusNode.textContent = message;
}

async function start() {
  if (typeof cheerpjInit !== "function") {
    setStatus("CheerpJ loader failed to initialize.");
    return;
  }

  try {
    viewportNode.style.height = `${Math.max(window.innerHeight * 0.72, 540)}px`;
    setStatus("Loading Java runtime...");
    await cheerpjInit({
      version: 17,
      status: "splash",
      clipboardMode: "java",
      javaProperties: ["quarker.web=true"],
    });

    cheerpjCreateDisplay(-1, -1, viewportNode);
    setStatus("Starting Quarker...");
    const exitCode = await cheerpjRunJar("/app/quarker.jar");
    setStatus(`Quarker exited with code ${exitCode}.`);
  } catch (error) {
    console.error(error);
    setStatus(`Startup failed: ${error instanceof Error ? error.message : String(error)}`);
  }
}

void start();