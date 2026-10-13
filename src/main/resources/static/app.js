const ROWS = 8;
const COLS = 5;

const sheetBody = document.getElementById('sheetBody');
const formulaBar = document.getElementById('formulaBar');

let gridState = null;
let selectedRow = 0;
let selectedCol = 2;
let editingCell = false;
let pendingFormulaCommit = false;
let formulaBarRow = 0;
let formulaBarCol = 0;

function isErrorDisplay(text) {
  return typeof text === 'string' && text.startsWith('#');
}

function buildGridSkeleton() {
  sheetBody.innerHTML = '';
  for (let r = 0; r < ROWS; r++) {
    const tr = document.createElement('tr');
    const rowHeader = document.createElement('th');
    rowHeader.className = 'row-header';
    rowHeader.textContent = String(r + 1);
    rowHeader.scope = 'row';
    tr.appendChild(rowHeader);

    for (let c = 0; c < COLS; c++) {
      const td = document.createElement('td');
      td.className = 'cell';
      td.dataset.row = String(r);
      td.dataset.col = String(c);
      td.tabIndex = 0;

      const wrapper = document.createElement('div');
      wrapper.className = 'cell-wrapper';

      const display = document.createElement('div');
      display.className = 'cell-display';

      const handle = document.createElement('span');
      handle.className = 'fill-handle';
      handle.setAttribute('aria-hidden', 'true');

      wrapper.appendChild(display);
      wrapper.appendChild(handle);
      td.appendChild(wrapper);

      td.addEventListener('mousedown', (e) => {
        if (e.target.classList.contains('fill-handle')) return;
        selectCell(r, c);
      });

      td.addEventListener('dblclick', () => {
        selectCell(r, c);
        startInlineEdit(r, c);
      });

      tr.appendChild(td);
    }
    sheetBody.appendChild(tr);
  }
}

function getCellElement(row, col) {
  return sheetBody.querySelector(`td.cell[data-row="${row}"][data-col="${col}"]`);
}

function selectCell(row, col, options = {}) {
  selectedRow = row;
  selectedCol = col;
  document.querySelectorAll('td.cell.selected').forEach((el) => el.classList.remove('selected'));
  const cell = getCellElement(row, col);
  if (cell) {
    cell.classList.add('selected');
    if (!options.skipFocus) {
      cell.focus();
    }
  }
  updateFormulaBarFromSelection();
}

function updateFormulaBarFromSelection() {
  if (!gridState || editingCell) return;
  const cell = gridState.cells[selectedRow][selectedCol];
  formulaBar.value = cell ? cell.input : '';
}

function renderGrid() {
  if (!gridState) return;
  for (let r = 0; r < ROWS; r++) {
    for (let c = 0; c < COLS; c++) {
      const cellData = gridState.cells[r][c];
      const td = getCellElement(r, c);
      if (!td || td.classList.contains('editing')) continue;
      const display = td.querySelector('.cell-display');
      const text = cellData.display ?? '';
      display.textContent = text;
      display.classList.toggle('error', isErrorDisplay(text));
    }
  }
  selectCell(selectedRow, selectedCol, { skipFocus: true });
}

async function fetchGrid() {
  const res = await fetch('/api/cells');
  if (!res.ok) throw new Error('Failed to load grid');
  gridState = await res.json();
  renderGrid();
}

async function commitCell(row, col, value) {
  const res = await fetch('/api/cells/update', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ row, column: col, value }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || 'Update failed');
  }
  gridState = await res.json();
  renderGrid();
}

function removeEditor(td) {
  const editor = td.querySelector('.cell-editor');
  if (editor) editor.remove();
  td.classList.remove('editing');
  editingCell = false;
}

function startInlineEdit(row, col) {
  const td = getCellElement(row, col);
  if (!td || td.classList.contains('editing')) return;

  editingCell = true;
  td.classList.add('editing');
  const input = document.createElement('input');
  input.type = 'text';
  input.className = 'cell-editor';
  const cellData = gridState.cells[row][col];
  input.value = cellData ? cellData.input : '';
  td.querySelector('.cell-wrapper').appendChild(input);
  input.focus();
  input.select();

  const finish = async (save) => {
    if (!td.classList.contains('editing')) return;
    const newValue = input.value;
    removeEditor(td);
    if (save) {
      try {
        await commitCell(row, col, newValue);
      } catch (e) {
        console.error(e);
        alert(e.message);
        await fetchGrid();
      }
    } else {
      renderGrid();
    }
  };

  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      finish(true);
    } else if (e.key === 'Escape') {
      e.preventDefault();
      finish(false);
    }
  });

  input.addEventListener('blur', () => {
    setTimeout(() => finish(true), 0);
  });
}

formulaBar.addEventListener('focus', () => {
  pendingFormulaCommit = false;
  formulaBarRow = selectedRow;
  formulaBarCol = selectedCol;
});

formulaBar.addEventListener('keydown', async (e) => {
  if (e.key === 'Enter') {
    e.preventDefault();
    pendingFormulaCommit = true;
    formulaBar.blur();
  }
});

formulaBar.addEventListener('blur', async () => {
  if (!gridState) return;
  const value = formulaBar.value;
  const row = formulaBarRow;
  const col = formulaBarCol;
  const current = gridState.cells[row][col]?.input ?? '';
  if (value === current && !pendingFormulaCommit) return;
  try {
    await commitCell(row, col, value);
  } catch (e) {
    console.error(e);
    alert(e.message);
    await fetchGrid();
  }
  pendingFormulaCommit = false;
});

document.addEventListener('keydown', (e) => {
  if (editingCell || document.activeElement === formulaBar) return;
  if (document.activeElement?.classList.contains('cell-editor')) return;

  if (e.key.length === 1 && !e.ctrlKey && !e.metaKey && !e.altKey) {
    startInlineEdit(selectedRow, selectedCol);
    const td = getCellElement(selectedRow, selectedCol);
    const editor = td?.querySelector('.cell-editor');
    if (editor) {
      editor.value = e.key;
      editor.setSelectionRange(1, 1);
    }
    e.preventDefault();
  } else if (e.key === 'Enter') {
    startInlineEdit(selectedRow, selectedCol);
    e.preventDefault();
  } else if (e.key === 'ArrowUp' && selectedRow > 0) {
    selectCell(selectedRow - 1, selectedCol);
    e.preventDefault();
  } else if (e.key === 'ArrowDown' && selectedRow < ROWS - 1) {
    selectCell(selectedRow + 1, selectedCol);
    e.preventDefault();
  } else if (e.key === 'ArrowLeft' && selectedCol > 0) {
    selectCell(selectedRow, selectedCol - 1);
    e.preventDefault();
  } else if (e.key === 'ArrowRight' && selectedCol < COLS - 1) {
    selectCell(selectedRow, selectedCol + 1);
    e.preventDefault();
  }
});

buildGridSkeleton();
selectCell(0, 2);
fetchGrid().catch((err) => {
  console.error(err);
  alert('Could not connect to the server. Start the Java backend on port 8080.');
});
