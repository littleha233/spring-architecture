const uidInput = document.getElementById('uid');
const generateBtn = document.getElementById('generateBtn');
const loadBtn = document.getElementById('loadBtn');
const messageBox = document.getElementById('message');
const latestBox = document.getElementById('latest');
const walletTbody = document.getElementById('walletTbody');

function showMessage(text) {
    messageBox.textContent = text || '';
}

async function extractErrorMessage(response, fallbackMessage) {
    try {
        const body = await response.json();
        if (body && typeof body.message === 'string' && body.message.trim()) {
            return body.message;
        }
        if (body && typeof body.detail === 'string' && body.detail.trim()) {
            return body.detail;
        }
    } catch (e) {
        // ignore json parse errors
    }

    try {
        const text = await response.text();
        if (text && text.trim()) {
            return text;
        }
    } catch (e) {
        // ignore text parse errors
    }

    return fallbackMessage;
}

function renderLatest(wallet) {
    if (!wallet) {
        latestBox.innerHTML = '<div class="k">No wallet generated yet.</div>';
        return;
    }

    latestBox.innerHTML = `
        <div class="kv"><div class="k">UID</div><div class="v">${wallet.uid}</div></div>
        <div class="kv"><div class="k">ID</div><div class="v">${wallet.id}</div></div>
        <div class="kv"><div class="k">Address</div><div class="v">${wallet.address}</div></div>
        <div class="kv"><div class="k">Create Time</div><div class="v">${new Date(wallet.createTime).toLocaleString()}</div></div>
    `;
}

function renderList(wallets) {
    walletTbody.innerHTML = '';
    if (!wallets || wallets.length === 0) {
        walletTbody.innerHTML = '<tr><td colspan="4">No data.</td></tr>';
        return;
    }

    wallets.forEach((wallet) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${wallet.uid}</td>
            <td>${wallet.id}</td>
            <td>${wallet.address}</td>
            <td>${new Date(wallet.createTime).toLocaleString()}</td>
        `;
        walletTbody.appendChild(tr);
    });
}

async function loadWallets() {
    const uid = uidInput.value.trim();
    const query = uid ? `?uid=${encodeURIComponent(uid)}` : '';
    const response = await fetch(`/api/eth-addresses${query}`);
    if (!response.ok) {
        throw new Error(await extractErrorMessage(response, 'Failed to load wallets'));
    }
    const wallets = await response.json();
    renderList(wallets);
}

async function generateWallet() {
    const uidText = uidInput.value.trim();
    const uid = Number(uidText);
    if (!uidText || !Number.isInteger(uid) || uid <= 0) {
        showMessage('Please enter a positive integer uid.');
        return;
    }

    generateBtn.disabled = true;
    showMessage('');
    try {
        const response = await fetch('/api/eth-addresses/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ uid })
        });
        if (!response.ok) {
            throw new Error(await extractErrorMessage(response, 'Failed to generate wallet'));
        }

        const wallet = await response.json();
        renderLatest(wallet);
        await loadWallets();
        showMessage('Generated and saved successfully.');
    } catch (error) {
        showMessage(error.message || 'Request failed');
    } finally {
        generateBtn.disabled = false;
    }
}

generateBtn.addEventListener('click', generateWallet);
loadBtn.addEventListener('click', async () => {
    showMessage('');
    try {
        await loadWallets();
    } catch (error) {
        showMessage(error.message || 'Request failed');
    }
});

loadWallets().catch((error) => {
    showMessage(error.message || 'Request failed');
});
