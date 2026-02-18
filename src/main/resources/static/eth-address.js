const userIdInput = document.getElementById('userId');
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
        <div class="kv"><div class="k">User ID</div><div class="v">${wallet.userId}</div></div>
        <div class="kv"><div class="k">Address</div><div class="v">${wallet.address}</div></div>
        <div class="kv"><div class="k">Public Key</div><div class="v">${wallet.publicKey}</div></div>
        <div class="kv"><div class="k">Private Key</div><div class="v">${wallet.privateKey}</div></div>
        <div class="kv"><div class="k">Created At</div><div class="v">${new Date(wallet.createdAt).toLocaleString()}</div></div>
    `;
}

function renderList(wallets) {
    walletTbody.innerHTML = '';
    if (!wallets || wallets.length === 0) {
        walletTbody.innerHTML = '<tr><td colspan="6">No data.</td></tr>';
        return;
    }

    wallets.forEach((wallet) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${wallet.id}</td>
            <td>${wallet.userId}</td>
            <td>${wallet.address}</td>
            <td>${wallet.publicKey}</td>
            <td>${wallet.privateKey}</td>
            <td>${new Date(wallet.createdAt).toLocaleString()}</td>
        `;
        walletTbody.appendChild(tr);
    });
}

async function loadWallets() {
    const userId = userIdInput.value.trim();
    const query = userId ? `?userId=${encodeURIComponent(userId)}` : '';
    const response = await fetch(`/api/eth-addresses${query}`);
    if (!response.ok) {
        throw new Error(await extractErrorMessage(response, 'Failed to load wallets'));
    }
    const wallets = await response.json();
    renderList(wallets);
}

async function generateWallet() {
    const userIdText = userIdInput.value.trim();
    const userId = Number(userIdText);
    if (!userIdText || !Number.isInteger(userId) || userId <= 0) {
        showMessage('Please enter a positive integer userId.');
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
            body: JSON.stringify({ userId })
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
