export default function ColorStyleMenu() {
	return (
			<div className="mode-switch" style={{ display: "flex", flexWrap: "wrap"}}>
				<button type="button" className="d-flex align-items-center" data-bs-theme-value="light" aria-pressed="false">
					<i className="bi bi-sun"></i>
				</button>
				<button type="button" className="d-flex align-items-center" data-bs-theme-value="dark" aria-pressed="false">
					<i className="bi bi-moon"></i>
				</button>
				<button type="button" className="d-flex align-items-center" data-bs-theme-value="auto" aria-pressed="false">
					<i className="bi bi-display"></i>
				</button>
			</div>
	);
}
