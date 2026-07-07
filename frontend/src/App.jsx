import { lazy, Suspense } from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import AuthBridge from "@/components/auth/AuthBridge";
import RequireAuth from "@/components/auth/RequireAuth";
import Navbar from "@/components/layout/Navbar";
import ErrorBoundary from "@/components/shared/ErrorBoundary";
import LoadingScreen from "@/components/shared/LoadingScreen";
import HomePage from "@/pages/HomePage";

const GroupsPage = lazy(() => import("@/pages/GroupsPage"));
const GroupDetailsPage = lazy(() => import("@/pages/GroupDetailsPage"));
const AddExpensePage = lazy(() => import("@/pages/AddExpensePage"));
const SettlementsPage = lazy(() => import("@/pages/SettlementsPage"));
const NotFoundPage = lazy(() => import("@/pages/NotFoundPage"));

function App() {
  return (
    <Router>
      <AuthBridge />
      <Navbar />
      <ErrorBoundary>
        <Suspense fallback={<LoadingScreen />}>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route element={<RequireAuth />}>
              <Route path="/groups" element={<GroupsPage />} />
              <Route path="/groups/:id" element={<GroupDetailsPage />} />
              <Route path="/groups/:id/expense" element={<AddExpensePage />} />
              <Route path="/groups/:id/settlements" element={<SettlementsPage />} />
            </Route>
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </Suspense>
      </ErrorBoundary>
    </Router>
  );
}

export default App;
