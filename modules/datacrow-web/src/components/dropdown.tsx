import { useState } from "react";
import { Button } from "react-bootstrap";

type DropdownProps = {
	title: string;
	options: number[];
	handleSelectOption: (option: string) => void;
};

export default function Dropdown({
	title,
	options,
	handleSelectOption,
}: DropdownProps) {
	const [isOpen, setIsOpen] = useState(false);

	const toggleDropdown = () => {
		setIsOpen(!isOpen);
	};

	return (
		<div className="relative">
			<Button onClick={toggleDropdown}>
				<span>{title}</span>
			</Button>

			{isOpen && (
				<ul style={{ display: "flex", flexWrap: "wrap" }}>
					{options.map((option) => (
						<a
							className="pagination-button"
							key={option}
							onClick={() => {
								handleSelectOption("" + option);
								setIsOpen(false);
							}}
						>
							{option}
						</a>
					))}
				</ul>
			)}
		</div>
	);
}
