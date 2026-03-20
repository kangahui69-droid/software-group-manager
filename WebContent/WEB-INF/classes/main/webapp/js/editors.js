/**
 * Quill Editor Initialization and Configuration
 * Handles the setup of the Quill editor and form submission.
 */

function initQuillEditor(initialContent) {
    const quill = new Quill('#editor-container', {
        theme: 'snow',
        placeholder: '开始输入内容...',
        modules: {
            toolbar: [
                [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
                ['bold', 'italic', 'underline', 'strike'],
                ['blockquote', 'code-block'],
                [{ 'list': 'ordered' }, { 'list': 'bullet' }],
                [{ 'script': 'sub' }, { 'script': 'super' }],
                [{ 'indent': '-1' }, { 'indent': '+1' }],
                [{ 'color': [] }, { 'background': [] }],
                ['link', 'image', 'video'],
                ['clean']
            ]
        }
    });

    // Set initial content if available
    if (initialContent) {
        quill.root.innerHTML = initialContent;
    }

    // Save Quill data to hidden input before form submission
    // Save Quill data to hidden input before form submission
    const form = document.querySelector('form');
    form.addEventListener('submit', function (e) {
        // Get HTML content
        const htmlContent = quill.root.innerHTML;
        
        const hiddenInput = document.getElementById('content-data');
        if (hiddenInput) {
            hiddenInput.value = htmlContent;
        } else {
            console.error('Hidden input #content-data not found!');
            e.preventDefault(); // Stop submission if input is missing
        }
    });
}
