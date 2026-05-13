<<<<<<< HEAD
import { RouterProvider } from 'react-router';
import { AuthProvider } from './contexts/auth';
import { router } from './router';
=======
import { RouterProvider } from "react-router";
import { AuthProvider } from "./contexts/auth";
import { router } from "./router";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  );
}

export default App;
