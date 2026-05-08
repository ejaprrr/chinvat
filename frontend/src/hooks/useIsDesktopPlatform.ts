import { useSyncExternalStore } from "react";

const desktopPlatformQuery = "(pointer: fine) and (hover: hover)";

function subscribe(onStoreChange: () => void) {
  if (typeof window === "undefined") {
    return () => undefined;
  }

  const mediaQueryList = window.matchMedia(desktopPlatformQuery);
  mediaQueryList.addEventListener("change", onStoreChange);

  return () => {
    mediaQueryList.removeEventListener("change", onStoreChange);
  };
}

function getSnapshot() {
  if (typeof window === "undefined") {
    return false;
  }

  return window.matchMedia(desktopPlatformQuery).matches;
}

function getServerSnapshot() {
  return false;
}

function useIsDesktopPlatform() {
  return useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
}

export default useIsDesktopPlatform;
