import toast from "react-hot-toast";
import { getErrorMessage } from "@/lib/getErrorMessage";

export function notifySuccess(message) {
  toast.success(message);
}

export function notifyError(err, fallback = "Something went wrong") {
  toast.error(getErrorMessage(err, fallback));
}
