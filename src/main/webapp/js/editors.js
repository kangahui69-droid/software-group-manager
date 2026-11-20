/** News editor with private, authenticated media uploads. */
function initQuillEditor(initialContent) {
    const form = document.getElementById('news-editor-form');
    const status = document.getElementById('news-media-status');
    const token = form.querySelector('[name="mediaToken"]').value;
    const list = document.getElementById('news-attachment-list');
    const attachmentInput = document.getElementById('news-attachment-input');
    let uploading = 0;

    async function upload(file, kind) {
        uploading++;
        status.textContent = '正在上传：' + file.name;
        try {
            const data = new FormData();
            data.append('file', file); data.append('kind', kind); data.append('token', token);
            const response = await fetch(form.dataset.mediaUrl, { method: 'POST', body: data });
            const result = await response.json();
            if (!response.ok) throw new Error(result.error || '上传失败');
            status.textContent = '上传完成：' + file.name;
            return result;
        } catch (error) {
            status.textContent = file.name + ' 上传失败：' + error.message;
            throw error;
        } finally { uploading--; }
    }

    const quill = new Quill('#editor-container', {
        theme: 'snow', placeholder: '开始输入内容...',
        modules: { toolbar: [
            [{ header: [1, 2, 3, 4, 5, 6, false] }],
            ['bold', 'italic', 'underline', 'strike'], ['blockquote', 'code-block'],
            [{ list: 'ordered' }, { list: 'bullet' }],
            [{ script: 'sub' }, { script: 'super' }],
            [{ indent: '-1' }, { indent: '+1' }],
            [{ color: [] }, { background: [] }], ['link', 'image'], ['clean']
        ] }
    });
    if (initialContent) quill.clipboard.dangerouslyPasteHTML(initialContent);

    quill.getModule('toolbar').addHandler('image', function () {
        const input = document.createElement('input');
        input.type = 'file'; input.accept = '.jpg,.jpeg,.png,.webp';
        input.onchange = async function () {
            const file = input.files && input.files[0];
            if (!file) return;
            try {
                const result = await upload(file, 'IMAGE');
                const selection = quill.getSelection(true);
                quill.insertEmbed(selection.index, 'image', result.url, 'user');
                quill.setSelection(selection.index + 1);
            } catch (_) { /* Error is shown beside the attachment field. */ }
        };
        input.click();
    });

    list.addEventListener('click', function (event) {
        if (event.target.matches('[data-remove-attachment]')) event.target.closest('li').remove();
    });
    attachmentInput.addEventListener('change', async function () {
        for (const file of attachmentInput.files) {
            try {
                const result = await upload(file, 'ATTACHMENT');
                const li = document.createElement('li');
                li.className = 'list-group-item d-flex justify-content-between align-items-center';
                const label = document.createElement('span'); label.textContent = result.name;
                const hidden = document.createElement('input');
                hidden.type = 'hidden'; hidden.name = 'attachmentIds'; hidden.value = result.id;
                const remove = document.createElement('button');
                remove.type = 'button'; remove.className = 'btn btn-sm btn-outline-danger';
                remove.setAttribute('data-remove-attachment', ''); remove.textContent = '移除';
                li.append(label, hidden, remove); list.appendChild(li);
            } catch (_) { /* Continue uploading other selected files. */ }
        }
        attachmentInput.value = '';
    });

    form.addEventListener('submit', function (event) {
        if (uploading) {
            event.preventDefault(); status.textContent = '请等待文件上传完成后再保存新闻'; return;
        }
        document.getElementById('content-data').value = quill.root.innerHTML;
    });
}
