import { lazy, Suspense } from "react";
import { Toaster } from "react-hot-toast";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import AuthBridge from "@/components/auth/AuthBridge";
import RequireAuth from "@/components/auth/RequireAuth";
import RequireRole from "@/components/auth/RequireRole";
import Navbar from "@/components/layout/Navbar";
import ErrorBoundary from "@/components/shared/ErrorBoundary";
import LoadingScreen from "@/components/shared/LoadingScreen";
import HomePage from "@/pages/HomePage";

const GroupsPage = lazy(() => import("@/pages/GroupsPage"));
const GroupDetailsPage = lazy(() => import("@/pages/GroupDetailsPage"));
const AddExpensePage = lazy(() => import("@/pages/AddExpensePage"));
const SettlementsPage = lazy(() => import("@/pages/SettlementsPage"));
const AdminUsersPage = lazy(() => import("@/pages/AdminUsersPage"));
const NotFoundPage = lazy(() => import("@/pages/NotFoundPage"));

function App() {
  return (
    <Router>
      <AuthBridge />
      <Navbar />
      <Toaster
        position="top-right"
        toastOptions={{
          className: "rounded-lg border border-slate-700 bg-slate-800 text-slate-100 shadow-md",
          success: {
            iconTheme: { primary: "var(--color-success-500)", secondary: "var(--color-surface)" },
          },
          error: {
            iconTheme: { primary: "var(--color-danger-500)", secondary: "var(--color-surface)" },
          },
        }}
      />
      <ErrorBoundary>
        <Suspense fallback={<LoadingScreen />}>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route element={<RequireAuth />}>
              <Route path="/groups" element={<GroupsPage />} />
              <Route path="/groups/:id" element={<GroupDetailsPage />} />
              <Route path="/groups/:id/expense" element={<AddExpensePage />} />
              <Route path="/groups/:id/settlements" element={<SettlementsPage />} />
              <Route element={<RequireRole role="ADMIN" />}>
                <Route path="/admin/users" element={<AdminUsersPage />} />
              </Route>
            </Route>
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </Suspense>
      </ErrorBoundary>
    </Router>
  );
}

export default App;
