import React, { useEffect, useState } from "react";
import api from "../../api/api";
import { useNavigate, useParams } from "react-router-dom";

const SettlementsForm = () => {
  const { id } = useParams();
  const groupId = id;
  const navigate = useNavigate();
  const [settlements, setSettlements] = useState([]);

  useEffect(() => {
    api
      .get(`/groups/${groupId}/settlements/calculate`)
      .then((response) => {
        setSettlements(response.data);
      })
      .catch((err) => console.error(err));
  }, [groupId]);

  return (
    <div className="min-h-screen bg-gradient-to-r from-yellow-400 via-yellow-300 to-yellow-200 p-6 flex flex-col items-center">
      <div className="w-full max-w-lg bg-yellow-100 bg-opacity-90 rounded-lg shadow-lg p-8">
        <h1 className="text-3xl font-extrabold text-yellow-900 mb-6 drop-shadow-md">
          Settlements
        </h1>

        {settlements.length === 0 ? (
          <p className="text-yellow-900 text-lg">
            No settlements calculated yet.
          </p>
        ) : (
          <ul className="space-y-3">
            {settlements.map((settlement, index) => (
              <li
                key={index}
                className="p-4 border border-yellow-300 rounded-md bg-yellow-200 shadow-sm text-yellow-900 font-semibold"
              >
                <span className="font-bold">{settlement.fromUser.name}</span>{" "}
                pays <span className="font-bold">{settlement.toUser.name}</span>{" "}
                <span className="text-yellow-700">
                  ${settlement.amount.toFixed(2)}
                </span>
              </li>
            ))}
          </ul>
        )}

        <button
          type="button"
          onClick={() => navigate(`/groups/${groupId}`)}
          className="mt-6 text-yellow-700 font-semibold hover:text-yellow-900 transition"
        >
          &larr; Back
        </button>
      </div>
    </div>
  );
};

export default SettlementsForm;
