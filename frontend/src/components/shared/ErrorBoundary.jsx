import { Component } from "react";
import Button from "@/components/shared/Button";

class ErrorBoundary extends Component {
  state = { hasError: false };

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, info) {
    console.error("Unhandled UI error", error, info);
  }

  handleReload = () => {
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex flex-col items-center justify-center gap-6 px-4">
          <p className="text-red-700 font-semibold text-lg text-center">
            Something went wrong. Please reload the page.
          </p>
          <Button onClick={this.handleReload}>Reload</Button>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
