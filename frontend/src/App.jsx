import ApiTokenSetter from "@/components/auth/ApiTokenSetter";
import AuthSync from "@/components/auth/AuthSync";
import Navbar from "@/components/layout/Navbar";
import AddExpensePage from "@/pages/AddExpensePage";
import GroupDetailsPage from "@/pages/GroupDetailsPage";
import GroupsPage from "@/pages/GroupsPage";
import HomePage from "@/pages/HomePage";
import SettlementsPage from "@/pages/SettlementsPage";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";

function App() {
  return (
    <Router>
      <AuthSync />
      <ApiTokenSetter />
      <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/groups" element={<GroupsPage />} />
        <Route path="/groups/:id" element={<GroupDetailsPage />} />
        <Route path="/groups/:id/expense" element={<AddExpensePage />} />
        <Route path="/groups/:id/settlements" element={<SettlementsPage />} />
      </Routes>
    </Router>
  );
}

export default App;
