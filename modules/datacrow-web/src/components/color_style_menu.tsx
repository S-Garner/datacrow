export default function ColorStyleMenu() {
	return (
			<div className="mode-switch" style={{ display: "flex", flexWrap: "wrap", right: "0px" }}>
				<button type="button" className="dropdown-item d-flex align-items-center" data-bs-theme-value="light" aria-pressed="false">
					<i className="bi bi-sun"></i>
				</button>
				<button type="button" className="dropdown-item d-flex align-items-center" data-bs-theme-value="dark" aria-pressed="false">
					<i className="bi bi-moon"></i>
				</button>
				<button type="button" className="dropdown-item d-flex align-items-center" data-bs-theme-value="auto" aria-pressed="false">
					<i className="bi bi-display"></i>
				</button>
			</div>
	);
}
