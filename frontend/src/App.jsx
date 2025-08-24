import "./App.css";
import AddExpenseForm from "./components/expenses/AddExpenseForm";
import GroupDetails from "./components/group/GroupDetails";
import Groups from "./components/group/Groups";
import Home from "./components/home/Home";
import Navbar from "./components/shared/Navbar";
import React from "react";
import SettlementsForm from "./components/settlements/SettlementsForm";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";

function App() {
  return (
    <React.Fragment>
      <Router>
        <Navbar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/groups" element={<Groups />} />
          <Route path="/groups/:id" element={<GroupDetails />} />
          <Route path="/groups/:id/expense" element={<AddExpenseForm />} />
          <Route path="/groups/:id/settlements" element={<SettlementsForm />} />
        </Routes>
      </Router>
    </React.Fragment>
  );
}

export default App;
