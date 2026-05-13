(function () {
  const statusEl = document.getElementById("status");

  function setStatus(text) {
    statusEl.textContent = text;
  }

  function launch() {
    setStatus("Starting Quarker...");
    try {
      if (typeof main === "function") {
        main([]);
        setStatus("Running.");
      } else {
        setStatus("Startup failed: TeaVM runtime entrypoint not found.");
      }
    } catch (error) {
      console.error(error);
      setStatus(`Startup failed: ${error instanceof Error ? error.message : String(error)}`);
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", launch, { once: true });
  } else {
    launch();
  }
})();
