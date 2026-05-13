(function () {
  const TEMPLATE = `
    <div class="quarker-shell" data-quarker-root>
      <div class="quarker-frame" role="application" aria-label="Quarker game" tabindex="0">
        <pre id="quarker-screen" class="quarker-screen" data-quarker-screen aria-live="polite"></pre>
        <p id="status" class="quarker-status" data-quarker-status>Preparing...</p>
      </div>
    </div>
  `;

  let hasBooted = false;

  function setStatus(text) {
    const statusEl = document.querySelector("[data-quarker-status]");
    if (statusEl) {
      statusEl.textContent = text;
    }
  }

  function bootQuarker() {
    if (hasBooted) {
      return;
    }
    hasBooted = true;
    setStatus("Starting Quarker...");
    try {
      if (typeof main === "function") {
        main([]);
      } else {
        setStatus("Startup failed: TeaVM runtime entrypoint not found.");
      }
    } catch (error) {
      console.error(error);
      setStatus(`Startup failed: ${error instanceof Error ? error.message : String(error)}`);
    }
  }

  class QuarkerGameElement extends HTMLElement {
    connectedCallback() {
      if (this.dataset.mounted === "true") {
        return;
      }
      this.dataset.mounted = "true";
      this.innerHTML = TEMPLATE;

      const frame = this.querySelector(".quarker-frame");
      if (frame) {
        frame.focus({ preventScroll: true });
        this.addEventListener("pointerdown", () => {
          frame.focus({ preventScroll: true });
        });
      }

      if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", bootQuarker, { once: true });
      } else {
        bootQuarker();
      }
    }
  }

  if (!customElements.get("quarker-game")) {
    customElements.define("quarker-game", QuarkerGameElement);
  }
})();
