/** Build URL for a design's primary (or first) image */
export function designImageUrl(design) {
  if (!design?.images?.length) return null
  const img = design.images.find(i => i.isPrimary) || design.images[0]
  return img?.imageName ? `/api/design-images/view/${encodeURIComponent(img.imageName)}` : null
}

/** Build URL from a design image record */
export function imageRecordUrl(image) {
  return image?.imageName
    ? `/api/design-images/view/${encodeURIComponent(image.imageName)}`
    : null
}
