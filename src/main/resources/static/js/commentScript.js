let currentPage = 0;
const pageSize = 5;
let isLastPage = false;

const $commentContent = $("#comment-content");
$commentContent.on("input", function() {
    const hasValue = $commentContent.val().trim().length > 0;
    $("#btnComment").prop("disabled", !hasValue);
});

// Load comments with pagination
function loadComments() {
    if (isLastPage) return;

    $.ajax({
        url: "/comments",
        method: "GET",
        data: {
            contentId: contentId,
            contentType: contentType,
            page: currentPage,
            size: pageSize
        },
        success: function (response) {
            renderComments(response.comments, currentPage !== 0);

            currentPage++;
            isLastPage = response.currentPage === response.totalPages - 1;

            toggleLoadMoreButton(response);
        },
        error: function(error) {
            $('#comment-list').html('<p style="text-align:center;color:#a0aec0;">Failed to load comments</p>');
        }
    });
}

// Render comments
function renderComments(comments, append = false) {
    const $commentsContainer = $('#comments-container');

    if (comments.length === 0) {
        $commentsContainer.html('<p style="text-align:center;padding:40px;color:#a0aec0;">No comments yet. Be the first to comment!</p>');
        return;
    }

    if (!append) $commentsContainer.empty();

    comments.forEach(comment => {
        $commentsContainer.append(createCommentHtml(comment));
    });
}

function toggleLoadMoreButton(response) {
    const $btn = $('#load-more-comments');

    if (response.totalPages <= 1 || response.currentPage == response.totalPages - 1) {
        $btn.hide();
    } else {
        $btn.show();
    }
}

// Create reply HTML
function createReplyHtml(reply, parentId) {
    const avatar = reply.avatarInitials;
    const avatarColor = reply.avatarColor;
    const date = formatDate(reply.createdAt);
    const isEdited = reply.createdAt != reply.updatedAt;
    const isOwner = reply.userId === userId;
    const isAdmin = userRole === 'ADMIN';

    return `
        <div class="reply-item" data-parent-id="${parentId}" data-item-id="${reply.id}" data-item-type="reply">
            <div class="reply-avatar" style="background-color: ${avatarColor}">
                <span class="user-initials-small">${avatar}</span>
            </div>
            <div class="comment-content">
                <div class="comment-header">
                    <strong class="comment-author">${reply.username}</strong>
                    <span class="comment-date">${date} ${isEdited ? '(edited)' : ''}</span>
                    ${isOwner || isAdmin ? createDropdownMenu(reply.id, 'reply', isOwner, isAdmin) : ''}
                </div>
                ${createEditableContent(reply.content, reply.id, 'reply')}
            </div>
        </div>
    `;
}

// Create comment HTML
function createCommentHtml(comment) {
    const isDeleted = comment.deleted;
    const username = isDeleted ? '[Deleted User]' : comment.username;
    const avatar = comment.avatarInitials;
    const avatarColor = comment.avatarColor;
    const date = formatDate(comment.createdAt);
    const isEdited = !isDeleted && (comment.createdAt != comment.updatedAt);
    const isOwner = !isDeleted && (comment.userId === userId);
    const isAdmin = userRole === 'ADMIN';

    return `
        <div class="comment" data-item-id="${comment.id}" data-item-type="comment">
            <div class="reply-avatar" style="background-color: ${avatarColor}">
                <span class="user-initials-small">${avatar}</span>
            </div>
            <div class="comment-content">
                <div class="comment-header">
                    <strong class="comment-author">${username}</strong>
                    <span class="comment-date">${date} ${isEdited ? '(edited)' : ''}</span>
                    ${isOwner || isAdmin ? createDropdownMenu(comment.id, 'comment', isOwner, isAdmin) : ''}
                </div>
                ${createEditableContent(comment.content, comment.id, 'comment', isDeleted)}
                <div class="comment-actions">
                    ${!isDeleted ? `
                        <button class="comment-action-btn" onclick="toggleReplyForm(${comment.id})">
                            <i class="bi bi-chat-dots me-1"></i> Reply
                        </button>
                    ` : ''}
                    ${comment.replyCount > 0 ? `
                        <button class="comment-action-btn" id="load-replies-${comment.id}"
                                onclick="toggleReplies(${comment.id})"
                                data-count="${comment.replyCount}">
                            View replies (${comment.replyCount})
                        </button>
                    ` : ''}
                </div>
            </div>
        </div>
        <div id="reply-form-${comment.id}" class="reply-form">
            <textarea class="edit-textarea" id="reply-text-${comment.id}" placeholder="Write your reply..."></textarea>
            <div class="reply-form-actions">
                <button class="btn-cancel" onclick="toggleReplyForm(${comment.id})">Cancel</button>
                <button class="btn-reply" onclick="submitReply(${comment.id})">Reply</button>
            </div>
        </div>
        <div id="replies-container-${comment.id}" class="replies-wrapper" style="display:none;">
            <div class="replies-list"></div>
            <div class="load-more-replies" style="display:none; text-align: center; padding: 10px;">
                <button class="btn-load-more" onclick="loadReplies(${comment.id})">Load more replies...</button>
            </div>
        </div>
    `;
}

// Helper function untuk membuat dropdown menu
function createDropdownMenu(id, type, isOwner, isAdmin) {
    return `
        <div class="dropdown ms-auto">
            <button class="btn btn-link p-1 ellipsis-btn" data-bs-toggle="dropdown">
                <i class="bi bi-three-dots-vertical"></i>
            </button>
            <ul class="dropdown-menu dropdown-menu-end">
                ${isOwner ? `
                    <li>
                        <button class="dropdown-item" onclick="editItem(${id}, '${type}')">
                            <i class="bi bi-pencil me-2"></i> Edit
                        </button>
                    </li>
                ` : ''}
                ${isOwner || isAdmin ? `
                    <li>
                        <button class="dropdown-item text-danger" onclick="deleteItem(${id}, '${type}')">
                            <i class="bi bi-trash me-2"></i> Delete
                        </button>
                    </li>
                ` : ''}
            </ul>
        </div>
    `;
}

// Helper function untuk membuat editable content
function createEditableContent(content, id, type, isDeleted = false) {
    const displayContent = isDeleted ? 'This comment has been deleted.' : escapeHtml(content);
    const textClass = isDeleted ? 'comment-text text-muted fst-italic' : 'comment-text';

    return `
        <div class="comment-text-container">
            <p class="${textClass}">${displayContent}</p>

            ${!isDeleted ? `
                <div class="comment-edit-form" style="display:none;">
                    <textarea class="edit-textarea">${escapeHtml(content)}</textarea>
                    <div class="edit-form-actions">
                        <button class="btn-cancel" onclick="cancelEdit(${id}, '${type}')">Cancel</button>
                        <button class="btn-save" onclick="saveItem(${id}, '${type}')">Save</button>
                    </div>
                </div>
            ` : ''}
        </div>
    `;
}

// Unified edit function
function editItem(id, type) {
    const $itemEl = $(`[data-item-id="${id}"][data-item-type="${type}"]`);
    const $textContainer = $itemEl.find('.comment-text-container');

    $textContainer.find('.comment-text').hide();
    $textContainer.find('.comment-edit-form').show();
    $textContainer.find('.edit-textarea').focus();
}

// Unified cancel edit function
function cancelEdit(id, type) {
    const $itemEl = $(`[data-item-id="${id}"][data-item-type="${type}"]`);
    const $textContainer = $itemEl.find('.comment-text-container');

    $textContainer.find('.comment-text').show();
    $textContainer.find('.comment-edit-form').hide();
}

// Unified save function
function saveItem(id, type) {
    const $itemEl = $(`[data-item-id="${id}"][data-item-type="${type}"]`);
    const $textContainer = $itemEl.find('.comment-text-container');
    const $editForm = $textContainer.find('.comment-edit-form');
    const newContent = $editForm.find('.edit-textarea').val().trim();

    if (!newContent) {
        alert(`${type.charAt(0).toUpperCase() + type.slice(1)} cannot be empty`);
        return;
    }

    $.ajax({
        url: `/comments/${id}`,
        method: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify({ content: newContent }),
        success: function(response) {
            const $textEl = $textContainer.find('.comment-text');
            $textEl.text(newContent).show();
            $editForm.hide();
        },
        error: function(xhr, status, error) {
            alert(`Failed to update ${type}`);
        }
    });
}

function updateReplyUI(parentId, delta) {
    let $btn = $(`#load-replies-${parentId}`);
    let $container = $(`#replies-container-${parentId}`);

    if (!$container.length) {
        $(`[data-item-id="${parentId}"]`).append(`
            <div id="replies-container-${parentId}" class="replies-wrapper" style="display:none;">
                <div class="replies-list"></div>
            </div>
        `);
        $container = $(`#replies-container-${parentId}`);
    }

    if (!$btn.length) {
        $(`[data-item-id="${parentId}"] .comment-actions`).append(`
            <button class="comment-action-btn"
                    id="load-replies-${parentId}"
                    data-count="0"
                    onclick="toggleReplies(${parentId})">
                View replies
            </button>
        `);
        $btn = $(`#load-replies-${parentId}`);
    }

    let count = $btn.data('count') ?? 0;
    count = Math.max(0, count + delta);

    $btn.data('count', count);

    if (count === 0) {
        $btn.remove();
        $container.slideUp();
        return;
    }

    $container.slideDown();
    $btn.text('Hide replies');
}

function deleteItem(id, type) {
    const $itemEl = $(`[data-item-id="${id}"][data-item-type="${type}"]`);

    $.ajax({
        url: `/comments/${id}`,
        method: 'DELETE',
        success: function(response) {
            if (type === 'comment') {
                const $btnReplies = $(`#load-replies-${id}`);
                const hasReplies = $btnReplies.length > 0;

                if (hasReplies) {
                    $itemEl.find('.comment-text').text('This comment has been deleted.').addClass('text-muted fst-italic');
                    $itemEl.find('.comment-avatar').text('?').css('background', '#ccc');
                    $itemEl.find('strong').text('[Deleted User]');

                    $itemEl.find('.dropdown').remove();
                    $itemEl.find('button[onclick^="toggleReplyForm"]').remove();

                } else {
                    $(`#reply-form-${id}, #replies-container-${id}`).remove();
                    $itemEl.fadeOut(300, function() { $(this).remove(); });
                }
            } else {
                const parentId = $itemEl.data('parent-id');

                $itemEl.fadeOut(300, function () {
                    $(this).remove();
                    updateReplyUI(parentId, -1);
                });
            }
        },
        error: function(xhr) {
            alert(xhr.responseText || `Failed to delete ${type}`);
        }
    });
}

// Toggle reply form
function toggleReplyForm(commentId) {
    const $replyForm = $(`#reply-form-${commentId}`);
    const isActive = $replyForm.hasClass('active');

    $('.reply-form.active').removeClass('active');

    if (!isActive) {
        $replyForm.addClass('active');
        $(`#reply-text-${commentId}`).focus();
    }
}

// Toggle replies visibility
function toggleReplies(commentId) {
    const $container = $(`#replies-container-${commentId}`);
    const $button = $(`#load-replies-${commentId}`);

    const count = parseInt($button.data('count')) || 0;

    if ($container.is(':visible')) {
        $container.slideUp();
        $button.text(`View replies (${count})`);
    } else {
        $container.slideDown();

        if ($(`#replies-container-${commentId} .replies-list`).is(':empty')) {
            loadReplies(commentId, 0);
        }

        $button.text('Hide replies');
    }
}

// Load replies
function loadReplies(commentId, page = 0, isAppend = true) {
    const $container = $(`#replies-container-${commentId}`);
    const $list = $container.find('.replies-list');
    const $btnMore = $container.find('.load-more-replies');

    $.ajax({
        url: `/comments/${commentId}/replies`,
        data: { page: page, size: 5 },
        success: function(response) {
            if (!isAppend) $list.empty();

            response.replies.forEach(reply => {
                $list.append(createReplyHtml(reply, commentId));
            });

            if (response.currentPage < response.totalPages - 1) {
                $btnMore.show();
                $btnMore.find('button').attr('onclick', `loadReplies(${commentId}, ${response.currentPage + 1})`);
            } else {
                $btnMore.hide();
            }
        }
    });
}

// Submit reply
function submitReply(commentId) {
    const $form = $(`#reply-form-${commentId}`);
    const content = $form.find('textarea').val().trim();

    if (!content) {
        alert('Please enter your reply');
        return;
    }

    $.ajax({
        url: '/comments',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ content: content, contentId: contentId, contentType: contentType, parentId: commentId }),
        success: function(response) {
            $form.find('textarea').val('');
            toggleReplyForm(commentId);
            updateReplyUI(commentId, +1);
            loadReplies(commentId, 0, false);
        },
        error: function(error) {
            alert('Failed to post reply. Please try again.');
        }
    });
}

// Submit comment
$('#comment-form').on('submit', function(e) {
    e.preventDefault();

    const content = $('#comment-content').val().trim();

    if (!content) {
        alert('Please enter your comment');
        return;
    }

    $.ajax({
        url: '/comments',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ content: content, contentId: contentId, contentType: contentType, parentId: null }),
        success: function(response) {
            $('#comment-content').val('');

            currentPage = 0;
            isLastPage = false;

            loadComments();
        },
        error: function(error) {
            alert('Failed to post comment. Please try again.');
        }
    });
});

// Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    const options = {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };
    return date.toLocaleDateString('en-US', options);
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}