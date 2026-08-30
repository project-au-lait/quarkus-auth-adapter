<script lang="ts">
  import apiHandler from '$lib/arch/api/ApiHandler';
  import { loginUserStore } from '$lib/arch/auth/LoginUserStore';

  type CallLog = {
    run: number;
    call: number;
    ok: boolean;
    durationMs: number;
    error?: string;
  };

  let parallelCalls = $state(5);
  let runs = $state(1);
  let intervalMs = $state(0);
  let isRunning = $state(false);
  let logs = $state<CallLog[]>([]);

  const total = $derived(logs.length);
  const success = $derived(logs.filter((row) => row.ok).length);
  const failure = $derived(logs.filter((row) => !row.ok).length);

  function clearLogs() {
    logs = [];
  }

  function invalidateAccessToken() {
    loginUserStore.setAccessToken('invalid-token');
  }

  async function callApi(run: number, call: number): Promise<void> {
    const start = Date.now();
    try {
      const res = await apiHandler.handle<string>(fetch, (api) => api.restricted.restrictedList());

      logs = [
        ...logs,
        {
          run,
          call,
          ok: !!res,
          durationMs: Date.now() - start
        }
      ];
    } catch (e) {
      logs = [
        ...logs,
        {
          run,
          call,
          ok: false,
          durationMs: Date.now() - start,
          error: e instanceof Error ? e.message : String(e)
        }
      ];
    }
  }

  async function runInvestigation() {
    isRunning = true;
    logs = [];

    try {
      for (let run = 1; run <= runs; run += 1) {
        const tasks: Promise<void>[] = [];
        for (let call = 1; call <= parallelCalls; call += 1) {
          tasks.push(callApi(run, call));
        }
        await Promise.all(tasks);

        if (run < runs && intervalMs > 0) {
          await new Promise((resolve) => setTimeout(resolve, intervalMs));
        }
      }
    } finally {
      isRunning = false;
    }
  }
</script>

<h1>Parallel Request Test</h1>
<p>
  Run <code>restricted</code> in parallel to observe outcomes during token refresh.
</p>

<article>
  <h2>Purpose</h2>
  <p>
    This page is for testing and investigation of authentication behavior, especially refresh timing
    and parallel request handling. It helps reproduce 401 recovery patterns and compare
    success/failure rates under concurrent calls.
  </p>

  <h2>Verification Steps</h2>
  <ol>
    <li>Login with a valid user account in another tab or before opening this page.</li>
    <li>Open this page via direct URL: <code>/parallel-request</code>.</li>
    <li>
      Set <code>Parallel calls per run</code>, <code>Runs</code>, and
      <code>Interval between runs</code>.
    </li>
    <li>Click <code>Run investigation</code> and confirm Total, Success, and Fail values.</li>
    <li>Click <code>Invalidate access token</code> to force a refresh path, then run again.</li>
    <li>Compare results between single call and parallel call scenarios.</li>
    <li>Use <code>Clear</code> before each new scenario to reset logs.</li>
  </ol>
</article>

<div>
  <label>
    Parallel calls per run
    <input id="parallel-calls" type="number" min="1" bind:value={parallelCalls} />
  </label>
  <label>
    Runs
    <input id="runs" type="number" min="1" bind:value={runs} />
  </label>
  <label>
    Interval between runs (ms)
    <input id="interval-ms" type="number" min="0" bind:value={intervalMs} />
  </label>
</div>

<div>
  <button id="run-investigation" onclick={runInvestigation} disabled={isRunning}
    >Run investigation</button
  >
  <button id="clear-logs" onclick={clearLogs} disabled={isRunning}>Clear</button>
  <button id="invalidate-token" onclick={invalidateAccessToken} disabled={isRunning}
    >Invalidate access token</button
  >
</div>

<article>
  <strong>Summary</strong>
  <dl>
    <dt>Total calls</dt>
    <dd id="total-calls">{total}</dd>
    <dt>Success</dt>
    <dd id="success-calls">{success}</dd>
    <dt>Fail</dt>
    <dd id="fail-calls">{failure}</dd>
  </dl>
</article>

<table>
  <thead>
    <tr>
      <th>Run</th>
      <th>Call</th>
      <th>Result</th>
      <th>Duration(ms)</th>
      <th>Error</th>
    </tr>
  </thead>
  <tbody>
    {#if logs.length === 0}
      <tr>
        <td colspan="5">No data yet.</td>
      </tr>
    {:else}
      {#each logs as row (row.run + '-' + row.call + '-' + row.durationMs)}
        <tr>
          <td>{row.run}</td>
          <td>{row.call}</td>
          <td>{row.ok ? 'OK' : 'FAIL'}</td>
          <td>{row.durationMs}</td>
          <td>{row.error ?? '-'}</td>
        </tr>
      {/each}
    {/if}
  </tbody>
</table>
