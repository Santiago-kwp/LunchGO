import { ref } from 'vue';

export const useImageGallery = (images = ref([])) => {
  const currentImageIndex = ref(0);

  const handlePrevImage = () => {
    currentImageIndex.value =
      (currentImageIndex.value - 1 + images.value.length) % images.value.length;
  };

  const handleNextImage = () => {
    currentImageIndex.value = (currentImageIndex.value + 1) % images.value.length;
  };

  const setImageIndex = (index) => {
    currentImageIndex.value = index;
  };

  return {
    currentImageIndex,
    handlePrevImage,
    handleNextImage,
    setImageIndex,
  };
};

export const useImageModal = () => {
  const isImageModalOpen = ref(false);
  const modalImageUrl = ref('');
  const modalImageIndex = ref(0);
  const modalImages = ref([]);

  const openImageModal = (images, index) => {
    modalImages.value = images.map((img) => (typeof img === 'object' ? img.url : img));
    modalImageIndex.value = index;
    modalImageUrl.value = modalImages.value[index];
    isImageModalOpen.value = true;
  };

  const closeImageModal = () => {
    isImageModalOpen.value = false;
  };

  const handleModalPrevImage = () => {
    modalImageIndex.value =
      (modalImageIndex.value - 1 + modalImages.value.length) % modalImages.value.length;
    modalImageUrl.value = modalImages.value[modalImageIndex.value];
  };

  const handleModalNextImage = () => {
    modalImageIndex.value = (modalImageIndex.value + 1) % modalImages.value.length;
    modalImageUrl.value = modalImages.value[modalImageIndex.value];
  };

  return {
    isImageModalOpen,
    modalImageUrl,
    modalImageIndex,
    modalImages,
    openImageModal,
    closeImageModal,
    handleModalPrevImage,
    handleModalNextImage,
  };
};
