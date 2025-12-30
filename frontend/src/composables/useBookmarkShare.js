import axios from "axios";

export const useBookmarkShare = () => {
  const searchUserByEmail = (email) =>
    axios.get("/api/bookmark-links/search", { params: { email } });

  const searchUsersByEmail = (query) =>
    axios.get("/api/bookmark-links/search/list", { params: { query } });

  const requestLink = (requesterId, receiverId) =>
    axios.post("/api/bookmark-links", { requesterId, receiverId });

  const respondLink = (linkId, status) =>
    axios.patch(`/api/bookmark-links/${linkId}`, { status });

  const getSentLinks = (requesterId, status) =>
    axios.get("/api/bookmark-links/sent", {
      params: { requesterId, status },
    });

  const getReceivedLinks = (receiverId, status) =>
    axios.get("/api/bookmark-links/received", {
      params: { receiverId, status },
    });

  const deleteLink = (requesterId, receiverId) =>
    axios.delete("/api/bookmark-links", {
      params: { requesterId, receiverId },
    });

  const toggleBookmarkVisibility = (userId, restaurantId, isPublic) =>
    axios.patch("/api/bookmark/visibility", { userId, restaurantId, isPublic });

  const togglePromotionAgree = (userId, restaurantId, promotionAgree) =>
    axios.patch("/api/bookmark/promotion", null, {
      params: { userId, restaurantId, promotionAgree },
    });

  const getSharedBookmarks = (requesterId, targetUserId) =>
    axios.get("/api/bookmark/shared", {
      params: { requesterId, targetUserId },
    });

  const getMyBookmarks = (userId) =>
    axios.get("/api/bookmark/list", {
      params: { userId },
    });

  return {
    searchUserByEmail,
    searchUsersByEmail,
    requestLink,
    respondLink,
    getSentLinks,
    getReceivedLinks,
    deleteLink,
    toggleBookmarkVisibility,
    togglePromotionAgree,
    getSharedBookmarks,
    getMyBookmarks,
  };
};
