function App() {
  return (
    <main>
      <form>
        <h1>Login</h1>
        <p>Sign in to continue.</p>
        <input type="email" name="email" placeholder="Email" autoComplete="email" />
        <input
          type="password"
          name="password"
          placeholder="Password"
          autoComplete="current-password"
        />
        <button type="submit">Continue</button>
      </form>
    </main>
  )
}

export default App
